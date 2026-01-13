import { TestBed } from '@angular/core/testing';

import { AutitService } from './audit.service';

describe('AutitService', () => {
  let service: AutitService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AutitService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
